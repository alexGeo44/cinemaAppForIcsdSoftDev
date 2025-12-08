import { useEffect, useState } from "react";
import { screeningsApi } from "../../api/screenings.api";
import { Screening } from "../../domain/screenings/screening.types";
import { ScreeningCard } from "./ScreeningCard";
import { authStore } from "../../auth/auth.store";

export default function StaffReviewPage() {
  const user = authStore((s) => s.user);
  const [items, setItems] = useState<Screening[]>([]);

  useEffect(() => {
    if (!user) return;
    screeningsApi
      .byStaff({
        staffId: user.id,
        offset: 0,
        limit: 50,
      })
      .then((res) => setItems(res.data));
  }, [user]);

  if (!user) return <div>Must be logged in</div>;

  return (
    <div>
      <h1>Review queue</h1>
      <p style={{ fontSize: "0.9rem", opacity: 0.8 }}>
        Screenings assigned to you as STAFF.
      </p>
      <div style={{ marginTop: "1rem" }}>
        {items.map((s) => (
          <ScreeningCard key={s.id} screening={s} />
        ))}
        {items.length === 0 && <div>No screenings assigned.</div>}
      </div>
    </div>
  );
}
