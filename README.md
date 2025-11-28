## Payloads

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