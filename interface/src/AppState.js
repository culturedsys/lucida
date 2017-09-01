/**
 * A class to maintain application state (and instruct the view to re-render
 * itself when that state changes).
 */
export default class AppState {
  /**
   * Create an AppState object with default state
   *
   * @param fetch the fetch function to use
   * @param base the URL of the base against which to make requests
   * @param interval the number of milliseconds to wait between each check
   * on the status of an in-process request.
   */
  constructor(fetch, base, interval) {
    this.fetch = fetch;
    this.component = null;
    this.base = base;
    this.interval = interval;
  }

  /**
   * Set the component which should have its state updated when the state of
   * the app changes.
   */
  setComponent(component) {
    this.component = component
  }

  /**
   * Upload the files specified in the given formdata, and set up a check
   * for a response from the server.
   */
  handleUpload(formdata) {
    const fromFile = formdata.get('fromFile');
    const toFile = formdata.get('toFile');
    if (fromFile.name === "" || toFile.name === "") {
      this.component.setState({
        error: 'Select two documents to upload and compare',
        serverSideError: false
      });
      return;
    }

    this.component.setState({waiting: true, completed: false, error: null});

    const start = Date.now();

    this.fetch(this.base, {
      method: 'POST',
      body: formdata
    }).then((response) => response.json())
    .then((json) => {
      let requestUrl = new URL(json[0], this.base);

      let checkUpload = () => {
        this.fetch(requestUrl).then((response) =>{
          if (response.status === 200) {
            return response.json().then((body) => {

              const end = Date.now();
              console.debug(`Time taken to process: ${end - start}`);

              const [from, to] = body;

              this.component.setState({
                from: from, to: to, completed: true, waiting: false
              });

              clearInterval(interval);
            });
          } else if (response.status === 202) {
            this.component.setState({waiting: true});
          } else {
            return response.json().then((body) => {
              console.log(body);
              if (body.error !== undefined)
                throw new ServerError(body.error);
              else
                throw new ServerError("Unknown error");
            });
          }
        }).catch((e) => {
          clearInterval(interval);
          this.showError(e)
        });
      };

      let interval = setInterval(checkUpload, this.interval);
    }).catch((e) => this.showError(e));
  }

  showError(e) {
    this.component.setState({
      completed: false,
      waiting: false,
      error: e.message,
      serverSideError: true
    })
  }
}

/**
 * Represents an error reported from the server (as opposed to an error
 * contacting the server, which is reported as a TypeError thrown by the
 * fetch function).
 */
class ServerError extends Error { }
