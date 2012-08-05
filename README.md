fasm-compiler-plugin
====================

Maven plugin to compile [flat assembler](http://flatassembler.net/) source files (*.asm)

## Usage

Put your ASM sources in ```src/main/asm``` and include this plugin in you POM:

```
<build>
	<plugins>
		<plugin>
			<groupId>com.github.sarxos</groupId>
			<artifactId>fasm-compiler-plugin</artifactId>
			<version>0.0.1-SNAPSHOT</version>
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
