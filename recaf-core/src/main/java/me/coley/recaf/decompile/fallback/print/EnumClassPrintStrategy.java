package me.coley.recaf.decompile.fallback.print;

import me.coley.recaf.decompile.fallback.model.ClassModel;
import me.coley.recaf.util.AccessFlag;
import me.coley.recaf.util.StringUtil;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class printing strategy for enum types.
 *
 * @author Matt Coley
 */
public class EnumClassPrintStrategy extends BasicClassPrintStrategy{
	@Override
	protected void appendDeclaration(Printer out, ClassModel model) {
		int acc = model.getAccess();
		// Get flag-set and remove 'enum' and 'final'.
		// We will add 'enum' ourselves, and 'final' is redundant.
		Set<AccessFlag> flagSet = AccessFlag.getApplicableFlags(AccessFlag.Type.CLASS, acc);
		flagSet.remove(AccessFlag.ACC_ENUM);
		flagSet.remove(AccessFlag.ACC_FINAL);
		String decFlagsString = AccessFlag.sortAndToString(AccessFlag.Type.CLASS, flagSet);
		StringBuilder sb = new StringBuilder();
		if (decFlagsString.isBlank()) {
			sb.append("enum ");
		} else {
			sb.append(decFlagsString)
					.append(" enum ");
		}
		sb.append(StringUtil.shortenPath(model.getName()));
		String superName = model.getSuperName();
		// Should normally extend enum. Technically bytecode allows for other types if those at runtime then
		// inherit from Enum.
		if (superName != null && !superName.equals("java/lang/Enum")) {
			sb.append(" extends ").append(StringUtil.shortenPath(superName));
		}
		if (model.getInterfaces().size() > 0)  {
			sb.append(" implements ");
			String interfaces = model.getInterfaces().stream()
					.map(StringUtil::shortenPath)
					.collect(Collectors.joining(", "));
			sb.append(interfaces);
		}
		out.appendLine(sb.toString());
	}
}
