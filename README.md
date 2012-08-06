fasm-compiler-plugin
====================

Maven plugin to compile [flat assembler](http://flatassembler.net) source files (*.asm)

## Usage

1. Download [FASM](http://flatassembler.net) and unzip it somewhere,
2. Set ```FASM_HOME``` environment variable pointing to the FASM binaries,
3. Put your ASM sources in ```src/main/asm```,
4. Use belows plugin configuration in your POM

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

Above example will compile all ```*.asm``` files to corresponding ```*.exe``` ones. You
can change ```<extension>``` to whatever you want (bin, dll, etc). 

Available goals:

* fasm-compiler:compile
* fasm-compiler:help  

Instead of setting ```FASM_HOME``` you can also set ```<fasm.home>``` property in your POM:

```xml
<properties>
	<fasm.home>C:\urs\fasm</fasm.home>
</properties>
```
