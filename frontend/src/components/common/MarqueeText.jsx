import { useRef, useState } from "react";
import "./MarqueeText.scss";

const MARQUEE_GAP = 24; // px, 두 복사본 사이 간격 (scss 의 gap 과 같아야 함)
const MARQUEE_SPEED = 35; // px/초

const canHover = () =>
  typeof window !== "undefined" &&
  window.matchMedia?.("(hover: hover)").matches;

const MarqueeText = ({ text, className = "" }) => {
  const outerRef = useRef(null);
  const textRef = useRef(null);
  const [marquee, setMarquee] = useState(null);

  const handleMouseEnter = () => {
    const outer = outerRef.current;
    const inner = textRef.current;
    if (!outer || !inner || !canHover()) return;
    if (outer.scrollWidth <= outer.clientWidth) return;
    const shift = inner.getBoundingClientRect().width + MARQUEE_GAP;
    setMarquee({ shift, duration: shift / MARQUEE_SPEED });
  };

  const handleMouseLeave = () => {
    setMarquee(null);
  };

  const outerClassName = [
    "marquee",
    marquee ? "marquee--running" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  const outerStyle = marquee
    ? {
        "--marquee-shift": `${marquee.shift}px`,
        "--marquee-duration": `${marquee.duration}s`,
      }
    : undefined;

  return (
    <span
      ref={outerRef}
      className={outerClassName}
      style={outerStyle}
      title={text}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      {marquee ? (
        <span className="marquee__track">
          <span ref={textRef} className="marquee__text">
            {text}
          </span>
          <span className="marquee__text" aria-hidden="true">
            {text}
          </span>
        </span>
      ) : (
        <span ref={textRef} className="marquee__text">
          {text}
        </span>
      )}
    </span>
  );
};

export default MarqueeText;
