/**
 * A replacement for the fetch function that returns test data rather than
 * actually accessing the network.
 */

const fakeDocument = {
  description: 'Fake document',
  change: 'unchanged',
  children: [
    {
      description: 'Changed child',
      change: 'changed',
      children: []
    },
    {
      description: 'Inserted child',
      change: 'inserted',
      children: []
    },
    {
      description: 'Deleted child',
      change: 'deleted',
      children: []
    }
  ]
}

/**
 * Create a fake fetch function, that mimics the window.fetch function, but
 * returns fake data. Returns a pair, the first element being the fake fetch
 * function, and the second is a list which will contain an entry for each
 * call made to the fetch function.
 *
 * @param status the status code to use for responses from fetch function.
 * If this is 200, the response will contain the fake data listed above;
 * otherwise, it will contain a JSON object of the form {error: "message"}.
 */
function makeFakeFetch(status) {
  const log = [];

  if (status === undefined)
    status = 200;

  const options = {
    status: status
  }

  function fakeFetch(url, init) {
    let method;

    if (init === undefined || init.method === undefined) {
      method = 'GET';
    } else {
      method = init.method;
    }

    let body;
    if (init === undefined || init.body === undefined) {
      body = null;
    } else {
      body = init.body;
    }
    log.push([method, url, body]);

    if ( method === 'GET') {
      let value;
      if (status === 200)
        value = [fakeDocument, fakeDocument];
      else
        value = {error: "A fake error"};

      let response = new Response(JSON.stringify(value), options);
      return Promise.resolve(response);
    } else {
      return Promise.resolve(new Response('["fake-id-string"]'), options);
    }
  }

  return [fakeFetch, log];
}

export default makeFakeFetch;