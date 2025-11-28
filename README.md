## Payloads

## Config

config file is `appliedwebhook-common.toml`

```toml
#The URL of the webhook to send messages to
webhookUrl = "http://127.0.0.1:5140/awh"
#The token to authenticate with the webhook
token = ""
#Only send webhook when player leave(true/false)
send_only_on_leave = false
#Time consumption threshold (min)
threshold = 1.0
```

In default config, the threshold is 10min, and only send webhook when player leave.
**When you install this mod first time, you need to config the webhookUrl.**

### JobFinishPayload
The payload of the job finish event.
Will be sent when a player finishes a job.

Using `POST` method with `application/json` content type.

```typescript
export type JobFinishPayload = {
  playerId: string;       // UUID → string
  itemId: string;         // String → string
  amount: number;         // long → number
  durationMs: number;     // long → number
  isOnline: boolean;      // boolean → boolean
  playerName: string;     // String → string
};
```