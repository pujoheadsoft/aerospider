// @flow
import type { counterStateType } from '../reducers/counter';

type actionType = {
  +type: string
};

export const INCREMENT_COUNTER = 'INCREMENT_COUNTER';
export const DECREMENT_COUNTER = 'DECREMENT_COUNTER';

export function increment() {
  return {
    type: INCREMENT_COUNTER
  };
}

export function decrement() {
  return {
    type: DECREMENT_COUNTER
  };
}

export function incrementIfOdd(counter: number) {

    console.log("increment odd")

    if (counter % 2 === 0) {
      return {type: "NONE"};
    }

    return {
      type: 'INCREMENT_COUNTER'
    };
}

export function incrementAsync() {
  console.log("async inc")
  return {
    type: 'INCREMENT_ASYNC'
  };
}
