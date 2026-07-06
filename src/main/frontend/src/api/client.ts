import Axios, { type AxiosRequestConfig } from 'axios'

const instance = Axios.create({ baseURL: '/bikube' })

instance.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            window.location.href = '/bikube/oauth2/authorization/keycloak-hugin'
        }
        return Promise.reject(error)
    }
)

export const apiClient = <T>(config: AxiosRequestConfig): Promise<T> =>
    instance(config).then(({ data }) => data)

export default apiClient
