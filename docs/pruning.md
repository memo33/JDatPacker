# Omitting unreachable objects

The purpose of this document is to specify which objects are not used by the game,
so they can safely be omitted from the Plugins.

## Definitions

An object is *unreachable* if there is no way the game would need it during game play (assuming the Plugins folder remains unchanged).
Otherwise, the object is *reachable*.

Examples: LD files are unreachable. Growable lot examplars are reachable.

Many object types are only reachable if there exists a (transitive) reference to them (explicitly or implicitly) by something reachable.

## Unreachable objects

- **Lot exemplars** are never considered unreachable.

- **Flora** is never considered unreachable (so that we do not need to deal with tree controllers or MMPs;
  some tree controllers include invisible uninstall helpers, so some models may still be referenced by the save files
  even if the flora itself cannot technically be placed anymore).

- **Flora families** are never considered unreachable (for the same reasons).

- **Prop/Building families** are unreachable if
  * not referenced by any lot configurations (lot exemplars/cohorts, T21 exemplars/cohorts)

- **Prop/Building exemplars** are unreachable if
  * not referenced by any lot configurations (lot exemplars/cohorts, T21 exemplars/cohorts)

  and
  * not part of a reachable prop/building family
    (this makes it necessary to properly resolve parent cohorts)

- **S3D** models are unreachable if
  * not referenced by reachable props or buildings or flora
    (explicitly or implicitly, so RKTs must be considered)

  and
  * having GID different from Maxis default GID `0xbadb57f1` (to avoid pruning network models, tunnel portals, automata, etc.)

- **FSH** textures are unreachable if
  * not referenced by reachable S3D models

  and
  * they are not reachable BAT nightlights
    (IID is reachable nightlight if `IID - 0x8000` is an FSH material referenced by a reachable S3D that is RKT1/xm)

  and
  * they have GID different from Maxis default GIDs:
    - `0x1abe787d` (miscellaneous: network textures, Maxis buildings, …)
    - `0x0986135e` (base/overlay texture)
    - `0x2BC2759a` (shadow mask)
    - `0x2a2458f9` (animation sprite props)
    - `0x49a593e7` (animation sprite non-props)
    - `0x891b0e1a` (terrain/foundation)
    - `0x46a006b0` (UI image)

  or
  * base/overlay textures (GID = `0x0986135e`) not referenced by any lot configurations
    (explicitly or implicitly, so wealth-dependent lot textures must be considered)

  and
  * IID > `0x0000ffff` (otherwise it might be a zoning overlay)

- **LD** files are unreachable (TID = `0x6be74c60`, GID = `0x6be74c60`) (lot data files created by Lot Editor)

- **JFIF** files are unreachable if TID = `0x74807101` or `0x74807102` (PIM/LE previews)

- **BMP** files are unreachable if TID = `0x66778001` or `0x66778002` (PIM/LE previews)

- **XML** files are unreachable if TID = `0x88777601` or `0x88777602` (plugin descriptors)

## Prop/building families

Prop/building families do not have defining files, but instead they are made up by their members,
i.e. prop/building exemplars that include the family ID as a `0x27812870 Building/prop Family` property (1 or more IDs).
For some families, there is a cohort file that gives the family a name, but this is optional.

## Flora families

Similarly, flora families are defined by the property `0xA8F149C5 kSC4FloraFamilyProperty` of flora exemplars.

## Lot exemplars

Exemplars with GID = `0xa8fbd372` (and exemplar type `LotConfigurations`).

## T21 exemplars

Exemplars with GID = `0x89ac5643` (and exemplar type `0x21`).

## Building exemplars

Exemplars with exemplar type `0x02 Buildings` and arbitrary GID.

## Prop exemplars

Exemplars with exemplar type `0x1E Prop` and arbitrary GID.

## Flora exemplars

Exemplars with exemplar type `0x0F Flora` and arbitrary GID (or just GID = `0xe83e0437`).

## LotConfigPropertyLotObject property

`[0/1/2] + [Uint32]*11 + [I]*n` where `n ≥ 1`

- 0: building
- 1: prop
- 2: base/overlay texture
- others are irrelevant

How does ObjectID/object family work? (Jondor's T21 Editor sets it to random values.)

## Retaining walls types

Irrelevant

## Building foundations

Are defined through exemplar type `Foundations`.
(The FSHs have unique GIDs and we always consider them reachable, so foundations do no need to be considered.)

## LotConfigPropertyFamily

Lot families? Functionality presumably still unknown.
(According to a RippleJet post, these tend to block residentials if they point to a Maxis family, but do not affect commercials or industrials.)
They are irrelevant to us since we always consider lots to be reachable.

## Resource Key Types

- RKT0/Misc: `T,G,I`: explicitly references a single S3D
- RKT1/xm: `T,G,I` (regular BATs):
  implicitly references S3Ds `= I + {0,0x10,0x20,0x30} + {0,0x100,…,0x400}` (rotation, zoom)
  and nightlight FSH masks `= J + {0,0x8000}` where `J` is referenced as material by the S3Ds
- RKT2/xm: `T,G,I0,…,I19`: explicitly references S3Ds (rotation and zoom)
- RKT3/xm: `T,G,I0,…,I4`: explicitly references S3Ds (zoom)
- RKT4/xm: `[state,x,z,y,RKT0/1,T,G,I] * n`: references multiple RKT0 or RKT1
- RKT5/xm: `T,G,I`:
  implicitly references S3Ds `= I + {0x00,0x10,0x20,0x30} + {0x000,0x100,…,0x400} + {0,0x10000,0x20000}`
  (rotation, zoom, state (e.g. growable flora size))
  (4th digit of `I` does not necessarily need to be 0)
  (nightlights presumably do not exist for RKT5)

## Wealth-dependent base/overlay textures

If a type-2 lot object IID reference is of the form `I = 0x####0000`,
it implicitly references `I + {0,0x1000,0x2000,0x3000} + {0,0x100} + {0,0x10,…,0xf0} + {0,1,…,4}`
where
- 5th digit is wealth
- 6th digit is dilapidation
- 7th digit is unknown
- 8th digit is zoom

Else, if an IID reference is of the form `I = 0x#######0`, it implicitly references `I + {0,1,…,4}` for different zoom levels.

Else, an IID reference only explicilty references a single FSH file. (?)

TODO: This description may not be entirely accurate yet regarding the 7th digit, but it is usually 0 for wealth-dependent textures, so should not matter.

## Notes

Unverified assumptions regarding nightlights:
- Buildings: automatically use nightlight masks if present
- Props: use nightlight masks if `0x4A9F188B Light` property is set
  (we ignore this property, so we only remove nightlight masks of props that are truly unreachable)
