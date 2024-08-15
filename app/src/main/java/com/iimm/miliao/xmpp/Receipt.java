package com.iimm.miliao.xmpp;

import android.text.TextUtils;

import com.iimm.miliao.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.util.List;

public class Receipt extends IQ {
    private static final String ELEMENT = "body";
    private static final String NAMESPACE = "xmpp:tig:ack";
    private List<String> messageIdList;
    private Jid from;
    private Jid to;
    private String mLoginUserId;
    private XMPPTCPConnection mConnection;

    public Receipt(List<String> messageIdList) {
        super(ELEMENT, NAMESPACE);
        this.messageIdList = messageIdList;
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
        xml.rightAngleBracket().optAppend(TextUtils.join(",", messageIdList));
        return xml;
    }
}
