export default function Logo({ className = "" }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M20 4C11 4 5 9.5 5 17c0 1.2.2 2.3.5 3.3C14.5 20 20 14 20 4Z"
        fill="#b9f2cf"
      />
      <path
        d="M5.5 20.3C8.5 14 12.5 9.8 17.5 6.5"
        fill="none"
        stroke="#0b2a20"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </svg>
  );
}
