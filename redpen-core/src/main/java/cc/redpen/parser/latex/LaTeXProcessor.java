/**
 * redpen: a text inspection tool
 * Copyright (c) 2014-2015 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.redpen.parser.latex;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.pegdown.ast.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.ArrayDeque;

/**
 * Lame Pegdown adapter for the LaTeX parser.
 */
public class LaTeXProcessor {
    public RootNode parse(char[] stream) {
        final List<Token> tokens = new ArrayList<>();
        new StreamParser(stream, t -> tokens.add(t)).parse();
        return (P.walk (tokens));
    }

    /*package*/ static class P {
        public static RootNode walk(final List<Token> tokens) {
            final Deque<SuperNode> reg = new ArrayDeque<>();
            final Deque<Token> regTextiles = new ArrayDeque<>();

            final Runnable flush = () -> {
                if (!regTextiles.isEmpty()) {
                    reg.getLast().getChildren().add(new ParaNode(new TextNode(StringUtils.join(StreamParser.P.valuesOf(new LinkedList<>(regTextiles)), "")))); /* XXX */
                    regTextiles.clear();
                }
            };

            final RootNode o = new RootNode();
            reg.addLast(o);

            for (Token t : tokens) {
                final SuperNode scope = reg.getLast();

                switch (t.t) {
                case "PART":
                    flush.run();
                    reg.addLast(new HeaderNode(1, new TextNode(t.v)));
                    scope.getChildren().add(reg.getLast());
                    break;
                case "CHAPTER":
                    flush.run();
                    reg.addLast(new HeaderNode(2, new TextNode(t.v)));
                    scope.getChildren().add(reg.getLast());
                    break;
                case "SECTION":
                    flush.run();
                    reg.addLast(new HeaderNode(3, new TextNode(t.v)));
                    scope.getChildren().add(reg.getLast());
                    break;
                case "SUBSECTION":
                    flush.run();
                    reg.addLast(new HeaderNode(4, new TextNode(t.v)));
                    scope.getChildren().add(reg.getLast());
                    break;
                case "SUBSUBSECTION":
                    flush.run();
                    reg.addLast(new HeaderNode(5, new TextNode(t.v)));
                    scope.getChildren().add(reg.getLast());
                    break;
                case "ITEM":
                    flush.run();
                    if (t.v.length() > 0) {
                        scope.getChildren().add(new ParaNode(new TextNode(t.v)));
                    }
                    break;
                case "VERBATIM":
                    flush.run();
                    scope.getChildren().add(new ParaNode(new VerbatimNode(t.v)));
                    break;
                case "TEXTILE":
                    regTextiles.addLast(t);
                    break;
                }
            }

            flush.run();

            return o;
        }
    }
}
