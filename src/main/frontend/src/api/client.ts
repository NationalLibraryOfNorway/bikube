import axios from 'axios'

const apiClient = axios.create({ baseURL: '/bikube/api' })

apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            window.location.href = '/bikube/oauth2/authorization/keycloak-hugin'
        }
        return Promise.reject(error)
    }
)

export default apiClient
