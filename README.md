# Lucida - a tool for structural document comparison

Lucida is a web-based tool that displays the difference in structure between two
documents. A running instance can be accessed at http://lucida.cultured.systems/

The system is organised as a number of modules, each contained in a separate
subdirectory:

 * analysis - provides a service that detects structure of documents and
   differences betweens structures.
 * coordinator - provides a web service that controls communication between the
   interface and the analysis service.
 * interface - a browser-based interface to the system.
 * model - a shared library providing classes to represent the data model of the
   system (specifically, textual and formatting information about paragraphs).
 * protocol - a shared library providing utility functions for marshelling and
   unmarshelling document and change representations for communication between
   the interface, the coordinator, and the analysis service.
 * training - a Spark application to train the model used by the analysis
   service.

Additionally, the data directory provides labelled training data and the results
directory includes some statistics on the accuracy of the model. The project
directory is a standard SBT project information directory.

## Running

To run the application in development mode (with automated reloading of the
interface and coordinator components), you need to run three SBT tasks:
`interface/run`, `coordinator/run` and `analysis/run`. This will run the
service listening on port 3000.

## Deploying

Docker packaging is specified for the coordinator (which will include the
interface files) and analysis service, so a running system can be put together
by running:

 * `sbt interface/compile`
 * `sbt coordinator/docker:publishLocal`
 * `sbt analysis/docker:publishLocal`
 * `PLAY_APPLICATION_SECRET=secret docker-compose --file=deploy-local.yml up`

This will comile the application, package the coordinator and analysis servers
as docker images (called coordinator-component and analysis-component,
respectively) and run these services, with the coordinator listening on port
9000 of the local machine.

Note that the last line sets the PLAY_APPLICATION_SECRET environment variable,
which the Play framework uses for various HTTP security features (like session
and CSRF protection). I don't think this application makes use of any of these
features, but PLAY_APPLICATION_SECRET must be set for any Play application to
run in deploy mode (and you should probably set it to something actually
secret, rather than the word 'secret', in case some security feature I've
forgotten about is actually depending on it).

## Credits

All files in this repository, with the exception of the labelled training data
in the data directory (which come from the
[ParsCit](https://github.com/knmnyn/ParsCit) project) are the work of Tim
Fisken. This repository also links to [Intel's IMLLIB
repository](https://github.com/Intel-bigdata/imllib-spark), using git's
submodule facility; this was not written by Tim Fisken (credits are included in
the linked repository). 

