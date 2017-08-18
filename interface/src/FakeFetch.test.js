/**
 * Verify the behaviour of the fake test function
 */

import makeFakeFetch from './FakeFetch';

it("should record requests", () => {
  const [fetch, log] = makeFakeFetch();

  const url = 'fakeurl';

  fetch(url);

  expect(log).toContainEqual(['GET', url, null]);
});

it("should return a two-element array for GET", () => {
  const [fetch, log] = makeFakeFetch();

  let response = fetch("/fakeurl/", {method: 'GET'});

  expect(response.then(r => r.json())).resolves.toHaveLength(2);
});

it("should return a one-element array for POST", () => {
  const [fetch, log] = makeFakeFetch();

  let response = fetch("/fakeurl/", {method: 'POST'});

  expect(response.then(r => r.json())).resolves.toHaveLength(1);
});

it("should resolve to a 200 status by default", () => {
  const [fetch, log] = makeFakeFetch();

  const url = 'fakeurl';

  const response = fetch(url);

  expect(response.then(r => r.status)).resolves.toBe(200);
});

it("should resolve to the specified status", () => {
  const status = 404;
  const [fetch, log] = makeFakeFetch(status);

  const url = 'fakeurl';

  const response = fetch(url);

  expect(response.then(r => r.status)).resolves.toBe(status);
});