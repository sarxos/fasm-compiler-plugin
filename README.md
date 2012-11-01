fasm-compiler-plugin
====================

Maven plugin to compile [flat assembler (FASM)](http://flatassembler.net) asm files. It can use 
either FASM binaries from your PC or embedded ones from plugin JAR. Plugin should work on all
environments (like Linux, DOS, Windows), but was tested only on Windows. 

For example project please see ```example``` directory.

Plugin details can be found [here](http://fasm-compiler-plugin.sarxos.pl). 

### Use Local FASM Binaries

[Download](http://flatassembler.net/download.php) zipped FASM binaries and extract somewhere in
your local environment. Set ```FASM_HOME``` environment variable to point to the directory where 
FASM executable is located. Plugin will discover this variable automatically and use FASM from 
this location. 

### Use Embedded FASM Binaries

Do nothing, just use plugin in your POM.

## Usage

1. Put your ```ASM``` files in ```src/main/asm```,
2. Use this configuration in your POM:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.sarxos</groupId>
			<artifactId>fasm-compiler-plugin</artifactId>
			<version>0.2</version>
			<executions>
				<execution>
					<goals>
						<goal>compile</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<extension>exe</extension>
			</configuration>
		</plugin>
	</plugins>
</build>
```

Above example will compile all ```asm``` files to corresponding ```exe``` ones. You
can change ```<extension>``` to whatever you want (e.g. ```so```, ```dll```, ```bin```, etc).

Default location for compiled binaries is ```target/fasmbin```.

Available goals:

* fasm-compiler:compile
* fasm-compiler:help  

Instead of setting ```FASM_HOME``` you can also set ```<fasmHome>``` option in plugin configuration, e.g.:


```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.sarxos</groupId>
			<artifactId>fasm-compiler-plugin</artifactId>
			<version>0.2</version>
			<executions>
				<execution>
					<goals>
						<goal>compile</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<fasmHome>C:\urs\fasm-1.70.03</fasmHome>
				<extension>exe</extension>
			</configuration>
		</plugin>
	</plugins>
</build>
```

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
