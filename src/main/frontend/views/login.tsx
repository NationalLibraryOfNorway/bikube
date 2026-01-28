import {redirect} from "@/lib/utils";
import {useSearchParams} from "react-router";
import {ViewConfig} from "@vaadin/hilla-file-router/types.js";

export const config: ViewConfig = {
    menu: {
        exclude: true
    },
    loginRequired: false,  // This route should not require login
    title: 'Login'
};

export default function Login() {
    const [searchParams] = useSearchParams();
    const hasContinue = searchParams.has("continue");

    if(hasContinue) redirect('/hugin/');  // Context path is handled by servlet, use relative path

    return (<></>
    )
}
