// @flow
import { createActions } from 'redux-actions'

export const { fetchServerInfo, fetchServerInfoSuccess } = createActions({
    FETCH_SERVER_INFO: () => ({}),
    FETCH_SERVER_INFO_SUCCESS: () => ({})
});