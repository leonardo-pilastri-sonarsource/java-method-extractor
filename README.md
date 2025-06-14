# Java Method Extractor

Extracts all method definitions (including body) from `.java` files found in a given source — either a local directory 
or a public GitHub repository.

The output is a series of text files, each containing the methods found for a single Java file.

---

## 🛠 Features

- ✅ Recursively collects `.java` files
- ✅ Parses each file using Eclipse JDT
- ✅ Extracts all method bodies (with declaration)
- ✅ Outputs one file per source file in a `.txt` format
- ✅ Supports both local directory inspection and GitHub repo cloning

---

## 🚀 How to Use

#### Prerequisites

- Java 17 or later
- Maven (`mvn`) to build the project

---

#### 📦 Build the Executable JAR

```bash
mvn clean package
```

#### Running

After building, you can run the generated JAR with dependencies under the target folder:

```bash
java -jar java-method-extractor-1.0-jar-with-dependencies.jar <mode> <input> <output-dir>
```

| Mode       | Description                        | Example Input                      |
|------------|------------------------------------|------------------------------------|
| `--local`  | Use a local directory              | `/home/user/myproject`             |
| `--github` | Clone a public GitHub repo (HTTPS) | `https://github.com/user/repo.git` |

With `--github` mode, the GitHub repository specified as `<input>` will be cloned in a temporary directory.

`<output-dir>` is the path where all the text files will be generated.

```
output-dir/
├── Main.txt
├── Utils.txt
├── MyClass.txt
```
