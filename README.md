# Java Method Extractor

Extracts all method definitions (including body) from `.java` files found in a given source — either a local directory
or a public GitHub repository.

The output is a series of text files, each containing the methods found for a single Java file.

---

## 🛠 Features

- ✅ Recursively collects `.java` files
- ✅ Parses each file using Eclipse JDT or TreeSitter
- ✅ Extracts all method bodies (with declaration)
- ✅ Outputs one file per method in a `.txt` format
- ✅ Supports both local directory inspection and GitHub repo cloning in a temporary directory

---

## 🚀 How to Use

#### Prerequisites

- Java 17 or later: https://jdk.java.net/archive/#:~:text=17.0.2%20(build%2017.0.2%2B8)
- Maven (`mvn`) to build the project: https://maven.apache.org/download.cgi

---

#### 📦 Build the Executable JAR

From the root directory of the project, run:

```bash
mvn clean package
```

#### Running

After building, you can run the generated JAR with dependencies under the target folder:

From the root directory of the project, run:

```bash
java -jar target/java-method-extractor-1.0-jar-with-dependencies.jar <mode> <inputPath> <outputDir>
```

| Mode       | Description                        | Example Input Path                 |
|------------|------------------------------------|------------------------------------|
| `--local`  | Use a local directory              | `/home/user/myproject`             |
| `--github` | Clone a public GitHub repo (HTTPS) | `https://github.com/user/repo.git` |

With `--github` mode, the GitHub repository specified as `<inputPath>` will be cloned in a temporary directory.

`<outputDir>` is the path where all the text files will be generated.

```
outputDir/
├── Main.txt
├── Utils.txt
├── MyClass.txt
```

You can also specify which parser to use between the Eclipse Compiler for Java and TreeSitter with the respective flags:
`--ecj` or `--ts`.
By default, the Eclipse Compiler for Java is used.

You can also set the minimum number of lines for a method to be extracted with the `--ml <number>` flag.
By default, this is set to 0, meaning all methods will be extracted regardless of their length.

Use the flag `--oneline` if you wish the output methods to be in a single line.


At the end of the process, the output folder will also contain a `performance_metrics.txt` file of this format:
```
Performance Metrics:
--------------------
Total AST Generation Time: 259143600 ns  ~ 259.14 ms
Number of AST Generated: 16
Total Extraction Time: 7514500 ns ~ 7.51 ms
--of which normalization time: 5510900 ns ~ 5.51 ms
```