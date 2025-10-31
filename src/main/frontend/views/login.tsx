import {redirect} from "@/lib/utils";
import {useSearchParams} from "react-router";

export default function Login() {
    const [searchParams] = useSearchParams();
    const hasContinue = searchParams.has("continue");

    if(hasContinue) redirect('/bikube/hugin');

    return (<></>
    )
}
