# logmasker
Obfuscates IIS log files, without generating random values. This is especially useful if you want to share your log files with 
someone that is going to analyze them. The sensitive information like the username and client IP are obfuscated so that they
cannot be traced back to a person, but you still keep the correlation between different requests in the log file.

## Prerequisites
The tool requires Java 1.8 to run, everything else is contained in the zip archive.

## Installation
Extract the zip archive somewhere on your diskdrive.

## Usage
To run the application execute the following command on your terminal: 

``` shell
logmasker -in [input directory] -out [output directory]
```

The input directory contains all the logfiles you want to obfuscate. The output directory will contain the obfuscated log files.
Please make sure that you have the right permissions for both directories so that the tool can read/write files in them.

## Contributing
Feel free to fork and change stuff. If you think your change is useful for me, please make a pull request.
