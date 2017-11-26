// @flow
import 'whatwg-fetch'

class AerospikeDriver {
  constructor(host: string, port: number) {
    this.host = host;
    this.port = port;
  }

  fetch() {
    fetch(`http://${this.host}:${this.port}/v1/informations`)
      .then(function(response) {
        return response.json()
      }).then(function(json) {
        console.log('parsed json', json)
      }).catch(function(ex) {
        console.log('parsing failed', ex)
      })
  }
}

// var driver = new AerospikeDriver("localhost", 3100)
