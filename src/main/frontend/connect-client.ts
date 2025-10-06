import { ConnectClient as ConnectClient_1 } from "@vaadin/hilla-frontend";
import {MaybePromise, MiddlewareContext, MiddlewareNext} from "@vaadin/hilla-frontend/Connect.js";

const mwareFunction = async (context: MiddlewareContext, next: MiddlewareNext) => {
    if (["GET", "POST"].includes(context.request.method) && (context.request.url.includes("/ammo/connect/"))) {
        const newUrl = context.request.url.replace("/ammo", "")

        const body = context.request.method === 'POST'
            ? await context.request.clone().text()
            : undefined;

        context.request = new Request(newUrl, {
            method: context.request.method,
            headers: context.request.headers,
            body: body,
            credentials: context.request.credentials,
            cache: context.request.cache,
            redirect: context.request.redirect,
            referrer: context.request.referrer,
            referrerPolicy: context.request.referrerPolicy,
            mode: context.request.mode,
        });
    }
    return next(context);
}

const client_1 = new ConnectClient_1({prefix: "connect", middlewares: [mwareFunction]});
export default client_1;
