# What is JarStubGenerator?

JarStubGenerator is a project aimed at modifying jar files so the actual implementation stays hidden from the user and the jar can be published without needing to worry about leaking a not free-to-use jar.

# Motivation

In the space of Minecraft plugin development, some developers create plugins without APIs, and we need to use the jar to access their methods. This is not a safe thing to do as it can violate the license of the plugin.

# Usage

Download the jar from the GitHub releases page, and run it with:
`java -jar JarStubGenerator.jar <options>`

### Options

`-p/-path` - the path to the folder or the file containing the jar file(s)

`-o/-out` - the path to the output folder, defaults to `path`

`-s/-suffix` - the suffix of the generated jar

`-keep-fields` - keep all the fields of classes

`-f/-field-whitelist` - a list of field names separated by `;` e.g. `yourField;yourField2`

