// @flow
import { INCREMENT_COUNTER, DECREMENT_COUNTER } from '../actions/counter';

export type counterStateType = {
  +counter: number
};

type actionType = {
  +type: string
};

export default function counter(state: number = 0, action: actionType) {
  console.log("reducer counter: " + action.type)
  switch (action.type) {
    case INCREMENT_COUNTER:
      return state + 1;
    case DECREMENT_COUNTER:
      return state - 1;
    default:
      return state;
  }
}
