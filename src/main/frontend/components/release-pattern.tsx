interface ReleasePatternProps {
    releasePattern: number[];
}

const ReleasePattern = (props: ReleasePatternProps) => {

    const daysOfWeek = ['Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag', 'Søndag'];

    return (
        <div className="self-start mt-5">
            <h2 className="group-title-style mb-2">Utgivelsesmønster:</h2>
            <table className="table-fixed">
                <tbody className="text-left">
                {daysOfWeek.map((day, index) => (
                    <tr key={index}>
                        <td className="pr-3 font-bold">{day}:</td>
                        <td className='group-content-style'>{props.releasePattern[index]}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default ReleasePattern;
