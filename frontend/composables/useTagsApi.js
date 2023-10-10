import {useMainStore} from "~/store";

/**
 *
 * @param app {NuxtApp}
 */
export const useTagsApi = (app = undefined) => {
    return {
        /**
         * Get all tags for current user
         * @return {Promise<{data?: [String], error?: *}>}
         */
        async getTags() {
            console.log(`Try getTags`)
            const {data, error} = await useApi(app).getAuthed('/api/tags')

            if (!!error)
                return {error}

            useMainStore().setTags(data)

            return {data}
        },
    }
}