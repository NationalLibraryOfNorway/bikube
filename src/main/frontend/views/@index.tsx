import Logo from "@/components/logo";
import TitleSearch from "@/components/title-search";

export default function MainView() {

    return (
        <div className="flex flex-col items-center mt-25">
            <Logo className="w-[150px] mb-5"/>
            <TitleSearch className="p-3" />
        </div>
    )
};
