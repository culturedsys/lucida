/**
 * Tests for the Api class
 */

import Api from './Api';
import makeFakeFetch from './FakeFetch';

test("addRequest should post a request", () => {
  const [fetch, log] = makeFakeFetch();
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const formData = "formData";

  const response = api.addRequest(formData);

  expect(log).toContainEqual(['POST', base + 'requests/', formData]);
});

test("addRequest should resolve to a URL", () => {
  const [fetch, log] = makeFakeFetch();
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const formData = "formData";

  const response = api.addRequest(formData);

  expect(response).resolves.toBeInstanceOf(URL);
});

test("queryRequest should get the supplied URL", () => {
  const [fetch, log] = makeFakeFetch();
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const url = base + 'requests/fake-id';

  api.queryRequest(url);

  expect(log).toContainEqual(['GET', url, null]);
});

test("queryRequest should get a relative URL", () => {
  const [fetch, log] = makeFakeFetch();
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const url = 'fake-id';

  api.queryRequest(url);

  expect(log).toContainEqual(['GET', base + 'requests/' + url, null]);
});

test("queryRequest should resolve to a two-element array", () => {
  const [fetch, log] = makeFakeFetch();
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const url = base + 'requests/fake-id';

  const response = api.queryRequest(url);

  expect(response).resolves.toHaveLength(2);
});

test("queryRequest for an unfinished query should resolve to false", () => {
  const [fetch, log] = makeFakeFetch(202);
  const base = "http://fakeurl/";
  const api = new Api(base, fetch);

  const url = base + 'requests/fake-id';

  const response = api.queryRequest(url);

  expect(response).resolves.toBeFalsy();

});