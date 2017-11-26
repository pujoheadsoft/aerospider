import { delay } from 'redux-saga'
import { put, takeEvery } from 'redux-saga/effects'

export function* incrementAsync() {
  console.log("saga increment async")
  yield delay(1000)
  yield put({type: 'INCREMENT_COUNTER'})
}

export default function* rootSaga() {
  console.log("rootSaga")
  yield takeEvery('INCREMENT_ASYNC', incrementAsync)
}
