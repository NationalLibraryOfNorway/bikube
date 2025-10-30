package no.nb.bikube.api.newspaper.service

import jakarta.annotation.PreDestroy
import no.nb.bikube.api.core.exception.SearchIndexNotAvailableException
import no.nb.bikube.api.core.model.Title
import no.nb.bikube.api.core.util.logger
import org.apache.lucene.analysis.core.LowerCaseFilterFactory
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.WildcardQuery
import org.apache.lucene.store.FSDirectory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

interface TitleIndexService {
    fun indexAllTitles()
    fun addTitle(title: Title)
    fun searchTitle(query: String): List<Title>
}

@ConditionalOnProperty(
    prefix = "search-index",
    name = ["enabled"],
    havingValue = "true"
)
@Service
class TitleIndexServiceImpl(
    private val newspaperService: NewspaperService,
    @Value("\${search-index.path}") private val searchIndexPath: String
) : TitleIndexService {
    private val titleAnalyzer = CustomAnalyzer.builder()
        .withTokenizer(WhitespaceTokenizerFactory.NAME)
        .addTokenFilter(LowerCaseFilterFactory.NAME)
        .build()

    private val indexWriter = IndexWriter(
        FSDirectory.open(Paths.get(searchIndexPath)),
        IndexWriterConfig(titleAnalyzer)
    )

    private val searcherManager = SearcherManager(indexWriter, null)

    private fun makeDocument(title: Title): Document? {
        if (title.name == null)
            return null
        val document = Document()
        document.add(TextField("name", title.name, Field.Store.YES))
        document.add(StoredField("catalogueId", title.catalogueId))
        title.startDate?.let { document.add(StoredField("startDate", it.toString())) }
        title.endDate?.let { document.add(StoredField("endDate", it.toString())) }
        title.publisher?.let { document.add(StoredField("publisher", it)) }
        title.publisherPlace?.let { document.add(StoredField("publisherPlace", it)) }
        title.language?.let { document.add(StoredField("language", it)) }
        title.materialType?.let { document.add(StoredField("materialType", it)) }
        return document
    }

    private val indexStatus = AtomicInteger(IndexStatus.UNINITIALIZED.ordinal)

    @Scheduled(
        initialDelayString = "\${search-index.initial-delay}",
        fixedDelayString = "\${search-index.rebuild-index-delay}"
    )
    override fun indexAllTitles() {
        if (indexStatus.get() == IndexStatus.INDEXING.ordinal)
            return
        logger().debug("Start fetching all titles to index...")
        newspaperService.getAllTitles()
            .map { titles ->
                titles.mapNotNull { makeDocument(it) }
            }
            .doOnSuccess { documents ->
                indexStatus.set(IndexStatus.INDEXING.ordinal)
                indexWriter.deleteAll()
                indexWriter.addDocuments(documents)
                indexWriter.commit()
                searcherManager.maybeRefresh()
                indexStatus.set(IndexStatus.READY.ordinal)
                logger().info("Titles index ready")
            }
            .subscribe()
    }

    override fun addTitle(title: Title) {
        logger().debug("Adding title ${title.name} to index")
        indexWriter.addDocument(makeDocument(title))
        indexWriter.commit()
        searcherManager.maybeRefresh()
    }

    @Throws(SearchIndexNotAvailableException::class)
    override fun searchTitle(query: String): List<Title> {
        if (indexStatus.get() != IndexStatus.READY.ordinal)
            throw SearchIndexNotAvailableException()
        val indexSearcher = searcherManager.acquire()
        val terms = query.split(Regex("\\s+"))
        val queryBuilder = BooleanQuery.Builder()
        terms.forEach {
            queryBuilder.add(
                WildcardQuery(Term("name", "*${it.lowercase()}*")),
                BooleanClause.Occur.MUST
            )
        }

        val q = queryBuilder.build()
        logger().debug("Title search: {}", q)
        val storedFields = indexSearcher.storedFields()
        return indexSearcher.search(q, 50)
            .scoreDocs
            .map { storedFields.document(it.doc) }
            .map { doc ->
                Title(
                    catalogueId = doc.get("catalogueId"),
                    name = doc.get("name"),
                    startDate = doc.get("startDate")?.let { LocalDate.parse(it) },
                    endDate = doc.get("endDate")?.let { LocalDate.parse(it) },
                    publisher = doc.get("publisher"),
                    publisherPlace = doc.get("publisherPlace"),
                    language = doc.get("language"),
                    materialType = doc.get("materialType")
                )
            }
    }

    @Scheduled(fixedDelayString = "\${search-index.searcher-refresh-delay}")
    fun refresh() {
        searcherManager.maybeRefresh()
    }
    @PreDestroy
    fun close() {
        indexWriter.commit()
        indexWriter.close()
    }
}

@ConditionalOnProperty(
    prefix = "search-index",
    name = ["enabled"],
    havingValue = "false"
)
@Service
class TitleIndexServiceDisabledImpl : TitleIndexService {
    override fun indexAllTitles() {}
    override fun addTitle(title: Title) {}
    override fun searchTitle(query: String) = emptyList<Title>()
}

enum class IndexStatus {
    UNINITIALIZED,
    INDEXING,
    READY
}
