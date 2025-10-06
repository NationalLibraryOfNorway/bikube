import { useEffect, useState } from "react";
import { ArrowLeft, ArrowRight } from "lucide-react";

export default function ArrowNavigation({
  onPrev,
  onNext,
}: {
  onPrev: () => void;
  onNext: () => void;
}) {
  const [showLeft, setShowLeft] = useState(false);
  const [showRight, setShowRight] = useState(false);
  const [isTouch, setIsTouch] = useState(false);

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      const edgeThreshold = 80;
      setShowLeft(e.clientX < edgeThreshold);
      setShowRight(e.clientX > window.innerWidth - edgeThreshold);
    };

    const handleTouchStart = () => setIsTouch(true);

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "ArrowLeft") onPrev();
      if (e.key === "ArrowRight") onNext();
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("keydown", handleKeyDown);
    window.addEventListener("touchstart", handleTouchStart, { once: true });

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("keydown", handleKeyDown);
      window.removeEventListener("touchstart", handleTouchStart);
    };
  }, [onPrev, onNext]);

  if (isTouch) return null;

  return (
    <>
      {showLeft && (
        <button
          onClick={onPrev}
          className="fixed top-1/2 left-4 -translate-y-1/2 z-50 bg-muted text-foreground p-2 rounded-full shadow-md hover:bg-accent transition"
          aria-label="Previous"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
      )}
      {showRight && (
        <button
          onClick={onNext}
          className="fixed top-1/2 right-4 -translate-y-1/2 z-50 bg-muted text-foreground p-2 rounded-full shadow-md hover:bg-accent transition"
          aria-label="Next"
        >
          <ArrowRight className="w-5 h-5" />
        </button>
      )}
    </>
  );
}

