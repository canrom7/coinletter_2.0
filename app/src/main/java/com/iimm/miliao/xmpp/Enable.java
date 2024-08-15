package com.iimm.miliao.xmpp;

import com.iimm.miliao.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

public class Enable extends IQ {
    public static final String ELEMENT = "enable";
    public static final String NAMESPACE = "xmpp:tig:ack";
    private Jid from;
    private Jid to;
    private String mLoginUserId;
    private XMPPTCPConnection mConnection;

    public Enable() {
        super(ELEMENT, NAMESPACE);
        mConnection = XmppConnectionImpl.getInstance().getXMPPConnection();
        mLoginUserId = XmppStringUtil.parseName(mConnection.getUser().toString());
        from = JidCreate.entityFullFrom(
                Localpart.fromOrThrowUnchecked(mLoginUserId),
                mConnection.getXMPPServiceDomain(),
                mConnection.getConfiguration().getResource());
        to = mConnection.getXMPPServiceDomain();
        setFrom(from);
        setTo(to);
        setType(Type.set);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket().optAppend("enable");
        return xml;
    }
}
