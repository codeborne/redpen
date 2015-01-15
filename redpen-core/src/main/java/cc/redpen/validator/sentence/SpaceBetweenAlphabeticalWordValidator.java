package cc.redpen.validator.sentence;

import cc.redpen.RedPenException;
import cc.redpen.model.Sentence;
import cc.redpen.util.StringUtils;
import cc.redpen.validator.ValidationError;
import cc.redpen.validator.Validator;

import java.util.List;

import static cc.redpen.config.SymbolType.*;

public class SpaceBetweenAlphabeticalWordValidator extends Validator {
    private char leftParenthesis = '(';
    private char rightParenthesis = ')';
    private char comma = ',';

    @Override
    public void validate(List<ValidationError> errors, Sentence sentence) {
        char prevCharacter = ' ';
        int idx = 0;
        for (char character : sentence.getContent().toCharArray()) {
            if (!StringUtils.isBasicLatin(prevCharacter)
                    && prevCharacter != leftParenthesis && prevCharacter != comma
                    && StringUtils.isBasicLatin(character)
                    && Character.isLetter(character)) {
                errors.add(createValidationErrorWithPosition("Before", sentence,
                        sentence.getOffset(idx),
                        sentence.getOffset(idx+1)));
            } else if (
                    !StringUtils.isBasicLatin(character) && character != rightParenthesis
                            && StringUtils.isBasicLatin(prevCharacter)
                            && Character.isLetter(prevCharacter)) {
                errors.add(createValidationErrorWithPosition("After", sentence,
                        sentence.getOffset(idx),
                        sentence.getOffset(idx+1)));
            }
            prevCharacter = character;
            idx++;
        }
    }

    @Override
    protected void init() throws RedPenException {
        leftParenthesis = getSymbolTable().getSymbol(LEFT_PARENTHESIS).getValue();
        rightParenthesis = getSymbolTable().getSymbol(RIGHT_PARENTHESIS).getValue();
        comma = getSymbolTable().getSymbol(COMMA).getValue();
    }
}
