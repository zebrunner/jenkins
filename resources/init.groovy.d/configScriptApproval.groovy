import org.jenkinsci.plugins.scriptsecurity.scripts.*;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.*;

scriptApproval = ScriptApproval.get()
alreadyApproved = new HashSet<>(Arrays.asList(scriptApproval.getApprovedSignatures()))

approveSignature('method hudson.model.Cause$UpstreamCause getUpstreamProject')
approveSignature('method hudson.model.Run getCause java.lang.Class')
approveSignature('method org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper getRawBuild')

scriptApproval.save()

void approveSignature(String signature) {
    if (!alreadyApproved.contains(signature)) {
        scriptApproval.approveSignature(signature)
    }
}