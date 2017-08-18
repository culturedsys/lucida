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
      let response = new Response(JSON.stringify([fakeDocument, fakeDocument]), options);
      return Promise.resolve(response);
    } else {
      return Promise.resolve(new Response('["fake-id-string"]'), options);
    }
  }

  return [fakeFetch, log];
}

export default makeFakeFetch;