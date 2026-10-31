// TDlxHT Sent Messaging Service (@sentdm/sentdm)
// Multi-channel SMS, WhatsApp, and RCS messaging integration for task reminders and notifications

import SentDm from '@sentdm/sentdm';

let sentClient = null;

/**
 * Get or initialize the SentDm client instance.
 * Automatically utilizes SENT_DM_API_KEY environment variable.
 */
export function getSentClient() {
  const apiKey = process.env.SENT_DM_API_KEY;
  if (!apiKey) {
    return null;
  }
  if (!sentClient) {
    sentClient = new SentDm({ apiKey });
  }
  return sentClient;
}

/**
 * Send a notification or reminder message via Sent unified channels (SMS / WhatsApp / RCS).
 *
 * @param {Object} options
 * @param {string|string[]} options.to - Recipient phone number in E.164 format (e.g. "+1234567890")
 * @param {string} [options.templateName] - Template identifier
 * @param {Object} [options.parameters] - Template substitution parameters
 * @param {string} [options.body] - Raw text body (for direct message fallback)
 * @param {string[]} [options.channels] - Channel routing priority (e.g. ['whatsapp', 'sms', 'rcs'])
 * @returns {Promise<{success: boolean, response?: any, error?: string}>}
 */
export async function sendSentMessage({ to, templateName, parameters = {}, body, channels = ['whatsapp', 'sms', 'rcs'] }) {
  const client = getSentClient();
  const recipients = Array.isArray(to) ? to : [to];

  if (!client) {
    console.warn('[SentDm] SENT_DM_API_KEY is not configured in environment. Skipping delivery in sandbox mode.');
    return {
      success: false,
      error: 'SENT_DM_API_KEY_NOT_CONFIGURED',
      sandbox: true,
      simulatedTo: recipients
    };
  }

  try {
    const payload = {
      to: recipients,
      channel: channels
    };

    if (templateName) {
      payload.template = {
        name: templateName,
        parameters
      };
    } else if (body) {
      payload.body = body;
    }

    const response = await client.messages.send(payload);
    return { success: true, response };
  } catch (err) {
    console.error('[SentDm] Message dispatch error:', err);
    return { success: false, error: err.message || 'SENT_DISPATCH_FAILED' };
  }
}

