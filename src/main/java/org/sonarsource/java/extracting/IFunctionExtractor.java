package org.sonarsource.java.extracting;

import java.util.List;
import org.sonarsource.java.parsing.AstResult;

public interface IFunctionExtractor {

  List<FunctionInfo> extract(AstResult astResult, String source, int minLines);

}
