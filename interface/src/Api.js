/**
 * Abstracts calls to the server API.
 */

class Api {
  /**
   * Create an Api instance
   *
   * @param base the base URL against which to construct URLs for API calls
   * @param fetch a function that behaves like the standard window.fetch
   */
  constructor(base, fetch) {
    this.base = new URL('requests/', base);
    this.fetch = fetch;
  }

  /**
   * Posts a request to the server, containing the files in the supplied
   * form data.
   * @param formData a FormData object containing two form fields.
   * @return a promise that will resolve to the result of the request.
   */
  addRequest(formData) {
    const response = this.fetch(this.base.href, {
      method: 'POST',
      body: formData
    });

    return response.then(r => new URL(this.base, r.json[0]));
  }

  /**
   * Retrieves information about the status of the request identified by the
   * given url (which may be relative to the request endpoint). Returns a
   * promise which resolves to either an array of the file differences, or
   * false if the request has not yet been processed.
   *
   * @param url
   */
  queryRequest(url) {
    const absoluteUrl = new URL(url, this.base);
    const response = this.fetch(absoluteUrl.href);
    return response.then( r => {
      if (r.status === 202)
        return false;
      else
        return r.json();
    });
  }
}

export default Api;