# cwa-deidentyfier
Code for removing device id from actigraphy data

It currently only works with .cwa data.
It replaces only DeviceID in the metadata header and copies the rest intact.

## Usage

Donwload `deidentyfier.jar` from the releases.

1. Use the simple mode

```
java -jar deidentyfier.jar
``` 

Enter file to convert or directory with files to be converted when prompted.
See bellow for details of both actions.

2. To convert one file:

```
java -jar deidentyfier.jar file-to-convert.cwa
``` 

It will create `out/file-to-convert_noid.cwa` in the same folder as `file-to-convert.cwa`

3. To convert all cwa files in a directory

```
java -jar deidentyfier.jar dir-with-input-files
``` 

It will replace the device id for each of *.cwa files in the input directory.
The new files will be saved under `out` directory of the input one, with the names: 
`old-name_noid.cwa`

4. All available options are

* -source=FILE-OR_DIRECTORY - defines input file/directory for the conversions
* -dest=DIRECTORY - defines output directory for the converted files
* -sufix=SOME-SUFIX - defines what should be appended to the orignal name, 
for example `input.cwa` will become `inputSOME-SUFIX.cwa`


