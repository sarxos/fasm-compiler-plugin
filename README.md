fasm-compiler-plugin
====================

Maven plugin to compile [flat assembler](http://flatassembler.net) (asm) files

## Usage

1. Download [flat assembler](http://flatassembler.net/download.php) and unzip it somewhere on your disk,
2. Set ```FASM_HOME``` environment variable pointing to that location,
3. Put your ASM sources in ```src/main/asm```,
4. Use below's plugin configuration in your POM:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.sarxos</groupId>
			<artifactId>fasm-compiler-plugin</artifactId>
			<version>0.1</version>
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
can change ```<extension>``` to whatever you want (so, dll, bin, etc).

You can find compiled binaries in ```target/fasmbin``` directory.

Available goals:

* fasm-compiler:compile
* fasm-compiler:help  

Instead of setting ```FASM_HOME``` you can also set ```<fasm.home>``` property in your POM:

```xml
<properties>
	<fasm.home>C:\urs\fasm</fasm.home>
</properties>
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
