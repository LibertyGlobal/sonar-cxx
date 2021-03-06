/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "UsingNamespaceInHeader",
  name = "Using namespace directives are not allowed in header files",
  tags = {Tag.CONVENTION, Tag.PITFALL, Tag.BAD_PRACTICE},
  priority = Priority.BLOCKER,
  status = "DEPRECATED"
)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
//similar Vera++ rule T018
public class UsingNamespaceInHeaderCheck extends SquidCheck<Grammar> {

  private static final String[] DEFAULT_NAME_SUFFIX = new String[]{".h", ".hh", ".hpp", ".H"};

  private Boolean isHeader = false;

  private static boolean isHeader(String name) {
    for (String suff : DEFAULT_NAME_SUFFIX) {
      if (name.endsWith(suff)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.usingDirective);
  }

  @Override
  public void visitFile(AstNode astNode) {
    isHeader = isHeader(getContext().getFile().getName());
  }

  @Override
  public void visitNode(AstNode node) {
    if (isHeader) {
      // declaration directly in translation unit
      if (node.getParent().is(CxxGrammarImpl.declaration)
        && node.getParent().getParent().is(CxxGrammarImpl.translationUnit)) {
        final boolean containsNamespace = node.getChildren().stream()
          .anyMatch(childNode -> CxxKeyword.NAMESPACE.equals(childNode.getToken().getType()));
        if (containsNamespace) {
          getContext().createLineViolation(this, "Using namespace are not allowed in header files.", node);
        }
      }
    }
  }

}
