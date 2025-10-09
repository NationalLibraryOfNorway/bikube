import {ChangeEvent, FC} from 'react';
import {Table, TableBody, TableCell, TableHeader, TableRow, TableHead} from '@/components/ui/table';
import {Field} from "formik";
import NumberInputWithButtons from "@/components/number-input";
import {validateBetweenZeroAndFive} from "@/utils/validation-utils";

interface ReleasePatternProps {
    releasePattern: number[];
    handleChange: (e: ChangeEvent) => void;
    handleBlur: (e: FocusEvent) => void;
}

const ReleasePatternForm: FC<ReleasePatternProps> = ({releasePattern, handleChange, handleBlur}) => {
    const daysOfWeek = ['Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag', 'Søndag'];

    return (
        <Table
            className='table-fixed text-left mb-5'
            aria-labelledby='releaseTable'
        >
            <TableHeader>
                <TableRow>
                    <TableHead>Dag</TableHead>
                    <TableHead>Antall</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {daysOfWeek.map((day, index) => (
                    <TableRow key={index} className='text-left '>
                        <TableCell className='text-lg p-0'>{day}</TableCell>
                        <TableCell className='m-2.5 py-0 pr-0 w-full'>
                            <Field
                                name={`title.release_pattern[${index}]`}
                                value={releasePattern[index]}
                                component={NumberInputWithButtons}
                                className='input-number-style'
                                onChange={handleChange}
                                validate={validateBetweenZeroAndFive}
                                onBlur={handleBlur}
                                minValue={0}
                                maxValue={5}
                            />
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};

export default ReleasePatternForm;
