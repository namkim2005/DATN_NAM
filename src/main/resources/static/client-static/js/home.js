// Home page enhancements (placeholder)
(function(){
  // Smooth scroll to categories
  document.querySelectorAll('a[href^="#danh-muc"]').forEach(a => {
    a.addEventListener('click', function(e){
      e.preventDefault();
      const target = document.getElementById('danh-muc');
      if (target) target.scrollIntoView({behavior:'smooth'});
    });
  });

  // Reveal on scroll for sections and cards
  const revealEls = document.querySelectorAll('#hero, #sale-strip .alert, #danh-muc .card, #new-arrivals .ld-product, #best-sellers .ld-product, #promo .row');
  revealEls.forEach(el => el.classList.add('reveal'));

  const io = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        io.unobserve(entry.target);
      }
    });
  }, { threshold: 0.15 });

  revealEls.forEach(el => io.observe(el));

  // Subtle parallax on hero image on mouse move (desktop only)
  const heroImg = document.querySelector('#hero .ld-hero__img');
  if (heroImg && window.matchMedia('(pointer:fine)').matches) {
    const inner = heroImg.querySelector('img');
    let rafId;
    heroImg.addEventListener('mousemove', (e) => {
      const rect = heroImg.getBoundingClientRect();
      const dx = (e.clientX - rect.left) / rect.width - 0.5;
      const dy = (e.clientY - rect.top) / rect.height - 0.5;
      cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        inner.style.transform = `scale(1.03) translate(${dx * 6}px, ${dy * 6}px)`;
        inner.style.transition = 'transform .08s ease-out';
      });
    });
    heroImg.addEventListener('mouseleave', () => {
      inner.style.transform = '';
      inner.style.transition = 'transform .25s ease';
    });
  }
})(); 