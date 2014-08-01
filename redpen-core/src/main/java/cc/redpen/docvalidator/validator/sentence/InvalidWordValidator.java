/**
 * redpen: a text inspection tool
 * Copyright (C) 2014 Recruit Technologies Co., Ltd. and contributors
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
package cc.redpen.docvalidator.validator.sentence;

import cc.redpen.docvalidator.DocumentValidatorException;
import cc.redpen.docvalidator.ValidationError;
import cc.redpen.docvalidator.config.CharacterTable;
import cc.redpen.docvalidator.config.ValidatorConfiguration;
import cc.redpen.docvalidator.model.Sentence;
import cc.redpen.docvalidator.util.ResourceLoader;
import cc.redpen.docvalidator.util.WordListExtractor;
import cc.redpen.docvalidator.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Detect invalid word occurrences.
 */
public class InvalidWordValidator implements Validator<Sentence> {
  /**
   * Constructor.
   */
  public InvalidWordValidator() {
    invalidWords = new HashSet<>();
  }

  /**
   * Constructor
   * @param config Configuration object
   * @param characterTable  Character settings
   * @throws DocumentValidatorException
   */
  public InvalidWordValidator(ValidatorConfiguration config,
      CharacterTable characterTable)
      throws DocumentValidatorException {
    initialize(config, characterTable);
  }

  public List<ValidationError> validate(Sentence line) {
    List<ValidationError> result = new ArrayList<>();
    String content = line.content;
    //NOTE: only Ascii white space since this validator works for european languages.
    List<String> words = Arrays.asList(content.split(" "));
    for (String invalidWord : invalidWords) {
      if (words.contains(invalidWord)) {
        result.add(new ValidationError(
            this.getClass(),
            "Found invalid Word: \"" + invalidWord + "\"", line));
      }
    }
    return result;
  }

  /**
   * Add invalid element. This method is used for testing
   *
   * @param invalid invalid word to be added the list
   */
  public void addInvalid(String invalid) {
    invalidWords.add(invalid);
  }

  private boolean initialize(ValidatorConfiguration conf,
      CharacterTable characterTable)
      throws DocumentValidatorException {
    String lang = characterTable.getLang();
    WordListExtractor extractor = new WordListExtractor();
    ResourceLoader loader = new ResourceLoader(extractor);

    LOG.info("Loading default invalid word dictionary for " +
        "\"" + lang + "\".");
    String defaultDictionaryFile = DEFAULT_RESOURCE_PATH
        + "/invalid-word-" + lang + ".dat";
    if (loader.loadInternalResource(defaultDictionaryFile)) {
      LOG.info("Succeeded to load default dictionary.");
    } else {
      LOG.info("Failed to load default dictionary.");
    }

    String confFile = conf.getAttribute("dictionary");
    if (confFile == null || confFile.equals("")) {
      LOG.error("Dictionary file is not specified.");
    } else {
      LOG.info("user dictionary file is " + confFile);
      if (loader.loadExternalFile(confFile)) {
        LOG.info("Succeeded to load specified user dictionary.");
      } else {
        LOG.error("Failed to load user dictionary.");
      }
    }

    invalidWords = extractor.get();
    return true;
  }

  private static final String DEFAULT_RESOURCE_PATH =
      "default-resources/invalid-word";

  private Set<String> invalidWords;

  private static final Logger LOG =
      LoggerFactory.getLogger(InvalidWordValidator.class);
}