package ws.slink.statuspage.tools;

import com.atlassian.sal.api.auth.LoginUriProvider;
import ws.slink.statuspage.type.IncidentStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class Common {

    public static void redirectToLogin(LoginUriProvider loginUriProvider,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }
    private static URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
    public static String getDefaultStatusMessage(IncidentStatus status) {
        switch (status) {
            case INVESTIGATING:
                return "We are continuing to investigate this issue.";
            case IDENTIFIED:
                return "The issue has been identified and a fix is being implemented.";
            case MONITORING:
                return "A fix has been implemented and we are monitoring the results.";
            case RESOLVED:
                return "This incident has been resolved.";
            case SCHEDULED:
                return "We will be undergoing scheduled maintenance during this time.";
            case IN_PROGRESS:
                return "Scheduled maintenance is currently in progress. We will provide updates as necessary.";
            case VERIFYING:
                return "Verification is currently underway for the maintenance items.";
            case COMPLETED:
                return "The scheduled maintenance has been completed.";
            default:
                return "";

        }
    }
}
