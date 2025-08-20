
  const skins = [
    "ahri.jfif",
    "caitlyn.jpg",
    "aurora.jfif",
    "xayah.jfif"
  ];

  const random = skins[Math.floor(Math.random() * skins.length)];
  document.getElementById("skinImage").src = `img${random}`;


