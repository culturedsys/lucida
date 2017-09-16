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

All files in this repository, with the exception of the labelled training data
in the data directory (which come from the
[ParsCit](https://github.com/knmnyn/ParsCit) project) were created by Tim
Fisken. This repository also links to [Intel's IMLLIB
repository](https://github.com/Intel-bigdata/imllib-spark), using git's
submodule facility; this was not written by Tim Fisken (credits are included in
the linked repository). 