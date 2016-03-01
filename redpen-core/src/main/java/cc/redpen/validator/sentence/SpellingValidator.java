/**
 * redpen: a text inspection tool
 * Copyright (c) 2014-2015 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.redpen.validator.sentence;

import cc.redpen.RedPenException;
import cc.redpen.model.Sentence;
import cc.redpen.tokenizer.TokenElement;
import cc.redpen.util.SpellingUtils;
import cc.redpen.validator.Validator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SpellingValidator extends Validator {
    private static String skipCharacters = "[\\!-/:-@\\[-`{-~]";
    // TODO: replace more memory efficient data structure
    private Set<String> defaultDictionary;

    public SpellingValidator() {
        super("list", new HashSet<>());
    }

    @Override
    protected void init() throws RedPenException {
        defaultDictionary = SpellingUtils.getDictionary(getSymbolTable().getLang());

        Optional<String> userDictionaryFile = getConfigAttribute("dict");
        if (userDictionaryFile.isPresent()) {
            String f = userDictionaryFile.get();
            getSetAttribute("list").addAll(WORD_LIST_LOWERCASED.loadCachedFromFile(findFile(f), "SpellingValidator user dictionary"));
        }
    }

    @Override
    public void validate(Sentence sentence) {
        for (TokenElement token : sentence.getTokens()) {
            String surface = normalize(token.getSurface());
            if (surface.length() == 0) {
                continue;
            }

            if (!defaultDictionary.contains(surface) && !getSetAttribute("list").contains(surface)) {
                addLocalizedErrorFromToken(sentence, token);
            }
        }
    }

    private String normalize(String token) {
        return token.toLowerCase().replaceAll(skipCharacters, "");
    }
}
