package net.evilblock.twofactorauth.totp;

import net.evilblock.twofactorauth.TwoFactorAuth;
import org.apache.commons.lang.StringUtils;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class DisclaimerPrompt extends StringPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return StringUtils.join(TwoFactorAuth.getInstance().getDisclaimerPrompt(), ' ');
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if (input.equalsIgnoreCase(TwoFactorAuth.getInstance().getAgreeText())) {
			return new ScanMapPrompt();
		}

		for (String message : TwoFactorAuth.getInstance().getSetupAbortedMessages()) {
			context.getForWhom().sendRawMessage(message);
		}

		return Prompt.END_OF_CONVERSATION;
	}

}
