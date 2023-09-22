import {useMainStore} from '~/store'

export default defineNuxtPlugin(({ $pinia }) => {
    return {
        provide: {
            // @ts-ignore
            store: useMainStore($pinia)
        }
    }
})